Vagrant::Config.run do |config|

  #cluster nodes
  nodes = [
           ['virtual1', '33.33.33.10']
           #['virtual2', '33.33.33.11'],
           #['virtual3', '33.33.33.12']
          ]

  # extra configuration settings
  chef_settings = {

    # our elasticsearch settings
    :elasticsearch => {
      :owner => "vagrant",
      :group => "vagrant",
      :install_dir => "/home/vagrant",
      :nodes => nodes,
      :java_opts => "-server",
      :iface => "_eth1:ipv4_"},

    # cassandra settings
    :cassandra => {
      :owner => "vagrant",
      :group => "vagrant",
      :install_dir => "/home/vagrant",
      :nodes => nodes,
      :java_opts => "-server"}}

  nodes.each do |node_nick, node_name|

    config.vm.define node_nick do |config|
      config.vm.box = "lucid32"
      config.vm.network :hostonly, node_name

      config.vm.provision :chef_solo do |chef|
        chef.cookbooks_path = "cookbooks"
        chef.add_recipe "vagrant"
        chef.json.merge!(:meta => {:node_name => node_name})
        chef.json.merge!(chef_settings)
      end
    end
  end
end
